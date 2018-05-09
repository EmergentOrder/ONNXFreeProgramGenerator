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
import onnx.onnx.TensorProto
import collection.JavaConverters._
import spire.math.Number

object ONNXProgramGenerator extends App {

  val path = Paths.get("src/main/scala/ONNXProgram.scala");

  val params = ParamsMap.params
  val nodes = ParamsMap.nodes
  val nodeInputs = ParamsMap.nodeInputs
  val nodeOutputs = ParamsMap.nodeOutputs
  val outputs = ParamsMap.outputs
  val attributes = ParamsMap.attributes
//val sortedParamNames = params.keys.toSeq.sorted.map(x => "param_" + x)
  val ops = ParamsMap.ops
  val distinctOps = ops.distinct

  val nodesInputsOpsAndOutputs = (nodeInputs zip ops) zip nodeOutputs

  val fullSource = "package org.emergentorder.onnx\n\n" +
    "import freestyle.free._\n" +
    "import cats.free.{ Free, FreeApplicative } \n" +
//               "import example.Float16\n"
    "import freestyle.free.implicits._\n" +
    "import scala.language.higherKinds\n\n" +
    "@module trait Application {\n" +
    distinctOps
      .map { x =>
        "  val " + x + ": " + x.capitalize + "\n"
      }
      .mkString("") +
    "  val dataSource: DataSource\n" +
    "  import cats.implicits._\n" +
    //Omit return type here for now
    "  def program = \n" +
    //Body of program generated here
    "    for {\n" +
    //Assume one output for now
    //TODO: fix Bad node hardcode here
//                 "      nodeInput3 <- dataSource.inputData\n" +
    "      node1 <- dataSource.inputData\n" +
    params
      .map(x =>
        "      node" + x._1 + " <- "
          + "dataSource.getParams(\"" + x._1 + "\")\n")
      .mkString("") +
    (nodesInputsOpsAndOutputs zip attributes)
      .map { x =>
        val nodesOrParams = x._1._1._1.map(y => "node" + y)
        val longListFields = x._2
          .filter { y =>
            val fields = y.getAllFields.toArray
            fields(1)._2.isInstanceOf[java.util.List[Long]]
          }
          .map { y =>
            val fields = y.getAllFields.toArray
            val field = fields(1)._2.asInstanceOf[java.util.List[Long]]
            y.name + " = Some((Array(" + field.asScala.mkString(",") + ")))"
          }
        val longFields = x._2
          .filter { y =>
            val fields = y.getAllFields.toArray
            fields(1)._2.isInstanceOf[Long]
          }
          .map { y =>
            val fields = y.getAllFields.toArray
            val field = fields(1)._2.asInstanceOf[Long]
            y.name + " = Some((" + field.toInt + "))"
          }
        val stringFields = x._2
          .filter { y =>
            val fields = y.getAllFields.toArray
            fields(1)._2.isInstanceOf[String]
          }
          .map { y =>
            val fields = y.getAllFields.toArray
            val field = fields(1)._2.asInstanceOf[String]
            y.name + """ = Some(("""" + field + """"))"""
          }
        val tensorProtoFields = x._2
          .filter { y =>
            val fields = y.getAllFields.toArray
            fields(1)._2.isInstanceOf[TensorProto]
          }
          .map { y =>
            val fields = y.getAllFields.toArray
            val field = ParamsMap.onnxTensorProtoToArray(
              fields(1)._2.asInstanceOf[TensorProto])
            y.name + " = Some((Array(" + field.mkString(",") + ")))"
          }

        "      node" + x._1._2(0) + " <- " + x._1._1._2 + "." + x._1._1._2 + "(" +
          nodesOrParams.mkString(",") +
          (if (tensorProtoFields.size > 0) "," else "") +
          tensorProtoFields.mkString(",") +
          (if (longListFields.size > 0) "," else "") +
          longListFields.mkString(",") +
          (if (stringFields.size > 0) "," else "") +
          stringFields.mkString(",") +
          (if (longFields.size > 0) "," else "") +
          longFields.mkString(",") +
          ")\n"
      }
      .mkString("") +
    "    } yield (" +
    outputs.map(x => "node" + x.name).mkString(",") +
    ")\n" +
    "}\n"
//pw.write("for {\n")

  def generate() = {
    val onnxSource = fullSource.parse[Source].get

    Files.write(path, onnxSource.syntax.getBytes("UTF-8"));
  }

  generate()

}
