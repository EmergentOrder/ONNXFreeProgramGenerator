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
import onnx.onnx.ModelProto
import onnx.onnx.TensorProto
import collection.JavaConverters._
import spire.math.Number
import scala.reflect.ClassTag

object ParamsMap {

//TODO: Inject model
  org.bytedeco.javacpp.Loader.load(classOf[org.bytedeco.javacpp.onnx])

  val byteArray = Files.readAllBytes(Paths.get("single_relu.onnx"))

  val res = ModelProto.parseFrom(byteArray)

  val graph = res.getGraph

  def dimsToArray[T: ClassTag](dimsCount: Int,
                               dimsList: List[Long]): Array[T] = {
    val dimsArrayInt = dimsList.map(x => x.toInt).toArray
    val arrX = dimsCount match {
      case 1 => Array.ofDim[T](dimsArrayInt(0))
      case 2 => Array.ofDim[T](dimsArrayInt(0), dimsArrayInt(1)).flatten
      case 3 =>
        Array
          .ofDim[T](dimsArrayInt(0), dimsArrayInt(1), dimsArrayInt(2))
          .flatten
          .flatten
      case 4 =>
        Array
          .ofDim[T](dimsArrayInt(0),
                    dimsArrayInt(1),
                    dimsArrayInt(2),
                    dimsArrayInt(3))
          .flatten
          .flatten
          .flatten
      case 5 =>
        Array
          .ofDim[T](dimsArrayInt(0),
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

  def onnxTensorProtoToArray(tensorProto: TensorProto): Array[Number] = {
    val onnxDataType = tensorProto.dataType.toString
    val dimsCount = tensorProto.dims.size
    val dimsList = tensorProto.dims.toList

    val bytes = ByteBuffer.wrap(tensorProto.rawData.toByteArray)

    //FIXME : MOAR TYPES?
    val array = onnxDataType match {
      case "INT" => {
        val arrX = dimsToArray[Int](dimsCount, dimsList)
        bytes.asIntBuffer.get(arrX)
        arrX.map(x => Number(x))
      }
      case "FLOAT" => {
        val arrX = dimsToArray[Float](dimsCount, dimsList)
        bytes.asFloatBuffer.get(arrX)
        arrX.map(x => if (x.isNaN) Number(0f) else Number(x))
      }
    }
    array
  }

  def attributes = graph.node.map(x => x.attribute.toArray).toArray
  def ops = graph.node.map(x => x.opType).toArray

  def nodeInputs = graph.node.map(x => x.input).toArray.map { x =>
    x.toArray
      .map(y => y.asInstanceOf[String].replaceAll("/", "_"))
      .filter(x =>
        nodeNames.contains("input_" + x) || nodeNames
          .contains("param_" + x) || nodeNames.contains("output_" + x))
  }

  def nodeOutputs = graph.node.map(x => x.output).toArray.map { x =>
    x.toArray.map(y => y.asInstanceOf[String].replaceAll("/", "_"))
  }

  def outputs =
    graph.output.toArray.filter(x => nodeNames.contains("output_" + x.name))

  def nodeNames = nodes.map(y => y._1)

  def nodes = {
    val someNodes = graph.input.map { x =>
      if (params.keys exists (_.equals(x.name)))
        ("param_" + x.name, params.get(x.name).map(y => y._2))
      else ("input_" + x.name, params.get(x.name).map(y => y._2))
    } ++ nodeOutputs.flatten.map(y => ("output_" + y, None))
    someNodes
  }

  def params =
    graph.initializer.map { x =>
      val dimsList = x.dims.toList
      val arrX: Array[Number] = onnxTensorProtoToArray(x)
      x.name.replaceAll("/", "_") -> (arrX, dimsList)
    }.toMap

}
