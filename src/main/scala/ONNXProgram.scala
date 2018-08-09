package org.emergentorder.onnx

import freestyle.free._
import cats.free.{ Free, FreeApplicative } 
import freestyle.free.implicits._
import scala.language.higherKinds

@module trait Application {
  val Relu: Relu
  val dataSource: DataSource
  import cats.implicits._
  def program = 
    for {
      nodex <- dataSource.inputData
      nodey <- Relu.Relu(nodex)
    } yield (nodey)
}
