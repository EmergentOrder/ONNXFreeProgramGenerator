/*
 * ONNXFreestyleProgramGenerator
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
import scala.meta._
import org.bytedeco.javacpp.onnx.TensorProto
import collection.JavaConverters._
import spire.math.Number

import scala.reflect.ClassTag

object ONNXProgramGenerator extends App {
  val FS = false

  val fileName = args(0)
  val programName = fileName.stripSuffix(".onnx").capitalize + (if(FS) "FS" else "")
  val path = Paths.get("src/main/scala/" + programName + ".scala");

  val paramsMap = new ParamsMap(fileName)

  def fullSource[VV:spire.math.Numeric: ClassTag] = {
    val params = paramsMap.params[VV]
    val nodes = paramsMap.nodes[VV]
    val nodeInputs = paramsMap.nodeInputs[VV]
    val nodeOutputs = paramsMap.nodeOutputs
    val outputs = paramsMap.outputs[VV]
    val attributes = paramsMap.attributes
  //val sortedParamNames = params.keys.toSeq.sorted.map(x => "param_" + x)
    val ops = paramsMap.ops
    val distinctOps = ops.distinct

    val nodesInputsOpsAndOutputs = (nodeInputs zip ops) zip nodeOutputs

    "package org.emergentorder.onnx\n\n" +
    (if(FS) "import freestyle.free._\n" +
      "import cats.free.{ Free, FreeApplicative } \n" +
//               "import example.Float16\n"
      "import freestyle.free.implicits._\n" 
      else "import shapeless.syntax.std.tuple._\n" +
      "import cats._\n" +
      "import cats.data._\n" +
      "import cats.implicits._\n"
      )  +
    "import scala.reflect.ClassTag\n" +
    "import scala.language.higherKinds\n\n" +
    (if(FS) "@module " else "") + "trait " + programName + " {\n" +
    distinctOps
      .map { x =>
        "  val " + x + (if(FS) "FS" else "") + ": " + x.capitalize + (if(FS) "FS" else "") + "\n"
      }
      .mkString("") +
    "  val dataSource: DataSource" + (if(FS) "FS" else "") + "\n" +
    "  import cats.implicits._\n" +
    //Omit return type here for now
    "  def program[VV:spire.math.Numeric:ClassTag] = \n" +
    //Body of program generated here
    "    for {\n" +
    //Assume one output for now
    "      node" +
    nodeInputs(0)(0) +
    " <- " + (if (FS) "" else "NonEmptyList.of(") + "dataSource.inputData[VV]" + (if(FS) "" else ")") + "\n" +
    params
      .map(x =>
        "      node" + x._1 + " <- "
          + (if(FS) "" else "NonEmptyList.of(") + " dataSource.getParams[VV](\"" + x._1 + "\")" + (if(FS) "" else ")" ) + "\n")
      .mkString("") +
    (nodesInputsOpsAndOutputs zip attributes)
      .map { x =>
        val nodesOrParams = x._1._1._1.map(y => "node" + y + """, """" + y + """"""")

//        x._2.map(y => y.getAllFields.toArray).foreach(y => println(y(1)._2.getClass))

//        println(x._2.size)

          val longFields = x._2
          .filter { y => y.has_i
          }
          .map { y =>

            val field = y.i.asInstanceOf[Long]
            y.name.getString + """ = Some(("""" + field.toInt + """"))"""
          }

          val longListFields = x._2
          .filter { y =>
            val longListCount = y.ints_size
            val longListList = (0 until longListCount.toInt).map(z => y.ints(z)).toList
            !longListList.isEmpty  //|| longList(0).isInstanceOf[Long]
          }
          .map { y =>
            val longListCount = y.ints_size
            val longListList = (0 until longListCount.toInt).map(z => y.ints(z)).toList
            val field = longListList.toVector.asInstanceOf[Vector[Long]]
            y.name.getString + """ = Some((Array("""" + field.mkString("""","""") + """")))""" 
          }
        val stringFields = x._2
          .filter { y =>
            val stringCount = y.strings_size
            val stringList = (0 until stringCount.toInt).map(z => y.strings(z)).toList
            !stringList.isEmpty //stringList(1).isInstanceOf[String]
          }
          .map { y =>
            val stringCount = y.strings_size
            val stringList = (0 until stringCount.toInt).map(z => y.strings(z)).toList
            val field = stringList.asInstanceOf[String]
            y.name.getString + """ = Some(Array("""" + field + """"))"""
          }
        val tensorProtoFields = x._2
          .filter { y =>
            val tensorCount = y.tensors_size
            val tensorList = (0 until tensorCount.toInt).map(z => y.tensors(z)).toList
            //fields(1)._2.isInstanceOf[TensorProto]
            !tensorList.isEmpty //tensorList(1).isInstanceOf[TensorProto]
          }
          .map { y =>
            val tensorCount = y.tensors_size
            val tensorList = (0 until tensorCount.toInt).map(z => y.tensors(z)).toList
            val field = paramsMap.onnxTensorProtoToArray[VV](
              tensorList.asInstanceOf[TensorProto])
            y.name.getString + " = Some((Array(" + field.mkString(",") + ")))"
          }
       
        val opName = x._1._1._2
        val nodeName = x._1._2(0) 
        "      node" + nodeName + " <- " + (if(FS) "" else "NonEmptyList.of(") + opName + (if(FS) "FS" else "") + "." + opName + "1" + "[VV]" +
        "(" +
        """"""" + nodeName + """", """ + //assumes > 0 args
          nodesOrParams.mkString(",") +
          (if (tensorProtoFields.size > 0) "," else "") +
          tensorProtoFields.mkString(",") +
         (if (longListFields.size > 0) "," else "") +
          longListFields.mkString(",") +
          (if (stringFields.size > 0) "," else "") +
          stringFields.mkString(",") +
          (if (longFields.size > 0) "," else "") +
          longFields.mkString(",") +
          ")" + (if(FS) "" else ")") + "\n"
      }
      .mkString("") +
    "    } yield (" +
    outputs.map(x => "node" + x.name.getString).mkString(",") +
    ")\n" +
    "}\n"
  }
//pw.write("for {\n")

  def generate() = {
//    println(fullSource[Float])
    //Seems to not catch some things it should
    val onnxSource = fullSource[Float].parse[Source].get

    Files.write(path, onnxSource.syntax.getBytes("UTF-8"));
  }

  generate()

}
