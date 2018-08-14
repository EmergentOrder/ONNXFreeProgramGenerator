package org.emergentorder.onnx

import freestyle.free._
import cats.free.{ Free, FreeApplicative } 
import freestyle.free.implicits._
import scala.reflect.ClassTag
import scala.language.higherKinds

@module trait Application {
  val Relu: Relu
  val dataSource: DataSource
  import cats.implicits._
  def program[VV:spire.math.Numeric:ClassTag] = 
    for {
      nodex <- dataSource.inputData[VV]
      nodey <- Relu.Relu1[VV]("y", nodex, "x")
    } yield (nodey)
}
