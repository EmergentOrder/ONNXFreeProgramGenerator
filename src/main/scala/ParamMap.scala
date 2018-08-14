/*
 * ParamsMap - TODO: Rename
 * Copyright (c) 2018 Alexander Merritt
 * All rights reserved. 
 * This program is free software: you can redistribute it and/or modify
 *
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package org.emergentorder.onnx

import java.nio.file._
import java.nio.ByteBuffer
//import onnx.onnx.ModelProto
//import onnx.onnx.TensorProto
import collection.JavaConverters._
//import spire.math.Number
import scala.reflect.ClassTag

import org.bytedeco.javacpp._
import org.bytedeco.javacpp.onnx._

object ParamsMap {

//TODO: Inject model
  org.bytedeco.javacpp.Loader.load(classOf[org.bytedeco.javacpp.onnx])

  val byteArray = Files.readAllBytes(Paths.get("super_resolution.onnx"))

        val res = new ModelProto()
        ParseProtoFromBytes(res.asInstanceOf[MessageLite], new BytePointer(byteArray:_*), byteArray.length) 

//  val res = ModelProto.parseFrom(byteArray)

  val graph = res.graph


//  val maxOpsetVersion = res.opsetImport
  //println("max opset : " + maxOpsetVersion)
  //val maxOpsetVersion = res.opset_import(0).version

  def dimsToArray[VV:spire.math.Numeric: ClassTag](dimsCount: Int,
                               dimsList: List[Long]): Array[VV] = {
    val dimsArrayInt = dimsList.map(x => x.toInt).toArray
    val arrX = dimsCount match {
      case 1 => Array.ofDim[VV](dimsArrayInt(0))
      case 2 => Array.ofDim[VV](dimsArrayInt(0), dimsArrayInt(1)).flatten
      case 3 =>
        Array
          .ofDim[VV](dimsArrayInt(0), dimsArrayInt(1), dimsArrayInt(2))
          .flatten
          .flatten
      case 4 =>
        Array
          .ofDim[VV](dimsArrayInt(0),
                    dimsArrayInt(1),
                    dimsArrayInt(2),
                    dimsArrayInt(3))
          .flatten
          .flatten
          .flatten
      case 5 =>
        Array
          .ofDim[VV](dimsArrayInt(0),
                    dimsArrayInt(1),
                    dimsArrayInt(2),
                    dimsArrayInt(3),
                    dimsArrayInt(4))
          .flatten
          .flatten
          .flatten
          .flatten
    }
    arrX
  }

  def onnxTensorProtoToArray[VV:spire.math.Numeric:ClassTag](tensorProto: TensorProto): Array[VV] = {
    val onnxDataType = tensorProto.data_type
    val dimsCount = tensorProto.dims_size
    val dimsList = (0 until dimsCount.toInt).map(x => tensorProto.dims(x)).toList

    val bytesBuffer = tensorProto.raw_data.asByteBuffer
    val byteArray = new Array[Byte](bytesBuffer.capacity)
    val bytes = ByteBuffer.wrap(byteArray)

    //FIXME : MOAR TYPES?
    val array = onnxDataType match {
      case TensorProto.INT32 => {
        val arrX = dimsToArray[Int](dimsCount, dimsList)
        bytes.asIntBuffer.get(arrX)
        arrX.map(x => x.asInstanceOf[VV])
      }
      case TensorProto.FLOAT => {
        val arrX = dimsToArray[Float](dimsCount, dimsList)
        bytes.asFloatBuffer.get(arrX)
        arrX.map(y => if(y.isNaN) 0.0f else y).map(x => x.asInstanceOf[VV])
      }
    }
    array.toArray
  }

  val nodeCount = graph.node_size.toInt
  val node = (0 until nodeCount).map(x => graph.node(x)).toList





//  def attributes = node.map(x => x.attribute.toArray).toArray

  def attributes = node.map{x => 
    val attributeCount = x.attribute_size.toInt
    val attribute = (0 until attributeCount).map(y => x.attribute(y)).toArray
    attribute
  }.toArray

  def ops = node.map(x => x.op_type.getString).toArray

  def nodeInputs[VV:spire.math.Numeric:ClassTag] = node.map{x => 
                                                              val inputCount = x.input_size.toInt
                                                              val input = (0 until inputCount).map(y => graph.input(y)).toList

                                                            input
                                                            }.toArray.map { x =>
    x.toArray
      .map(y => y.name.getString.asInstanceOf[String].replaceAll("/", "_"))
      .filter(x =>
        nodeNames[VV].contains("input_" + x) || nodeNames[VV]
          .contains("param_" + x) || nodeNames[VV].contains("output_" + x))
  }

  def nodeOutputs = node.map{x =>
                             val outputCount = x.output_size.toInt
                             val output = (0 until outputCount).map(y => x.output(y)).toList

                             output
                             }.toArray.map { x =>
    x.toArray.map(y => y.getString.asInstanceOf[String].replaceAll("/", "_"))
  }

  val globalOutputCount = graph.output_size.toInt
  val globalOutput = (0 until globalOutputCount).map(x => graph.output(x)).toList



  def outputs[VV:spire.math.Numeric:ClassTag] = {
    val outputArray = globalOutput.toArray
//    outputArray.foreach(x => println(x.name.getString))
    outputArray.filter(x => nodeNames[VV].contains("output_" + x.name.getString))
  }

  def nodeNames[VV:spire.math.Numeric:ClassTag] = nodes[VV].map(y => y._1)

  val inputCount = graph.input_size.toInt
  val input = (0 until inputCount).map(x => graph.input(x)).toList


  def nodes[VV:spire.math.Numeric:ClassTag] = {
    val someNodes = input.map { x =>
      val name = x.name.getString
      if (params[VV].keys exists (_.equals(name)))
        ("param_" + name, params[VV].get(name).map(y => y._2))
      else ("input_" + name, params[VV].get(name).map(y => y._2))
    } ++ nodeOutputs.flatten.map(y => ("output_" + y, None))
    someNodes
  }

  val initializerCount = graph.initializer_size
  val initializer = (0 until initializerCount).map(x => graph.initializer(x)).toList

  def params[VV:spire.math.Numeric:ClassTag] =
    initializer.map { x =>
      val dimsCount = x.dims_size
      val dimsList = (0 until dimsCount.toInt).map(y => x.dims(y)).toList

//      val dimsList = x.dims.toList
      val arrX: Array[VV] = onnxTensorProtoToArray[VV](x)
      x.name.getString.replaceAll("/", "_") -> (arrX, dimsList)
    }.toMap

}
