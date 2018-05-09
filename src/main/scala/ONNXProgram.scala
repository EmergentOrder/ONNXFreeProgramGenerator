package org.emergentorder.onnx

import freestyle.free._
import cats.free.{ Free, FreeApplicative } 
import freestyle.free.implicits._
import scala.language.higherKinds

@module trait Application {
  val Conv: Conv
  val Add: Add
  val Relu: Relu
  val Reshape: Reshape
  val Transpose: Transpose
  val dataSource: DataSource
  import cats.implicits._
  def program = 
    for {
      node1 <- dataSource.inputData
      node8 <- dataSource.getParams("8")
      node4 <- dataSource.getParams("4")
      node9 <- dataSource.getParams("9")
      node5 <- dataSource.getParams("5")
      node6 <- dataSource.getParams("6")
      node2 <- dataSource.getParams("2")
      node7 <- dataSource.getParams("7")
      node3 <- dataSource.getParams("3")
      node11 <- Conv.Conv(node1,node2,group = Some((1)))
      node12 <- Add.Add(node11,node3,broadcast = Some((1)),axis = Some((1)))
      node13 <- Relu.Relu(node12)
      node15 <- Conv.Conv(node13,node4,group = Some((1)))
      node16 <- Add.Add(node15,node5,broadcast = Some((1)),axis = Some((1)))
      node17 <- Relu.Relu(node16)
      node19 <- Conv.Conv(node17,node6,group = Some((1)))
      node20 <- Add.Add(node19,node7,broadcast = Some((1)),axis = Some((1)))
      node21 <- Relu.Relu(node20)
      node23 <- Conv.Conv(node21,node8,group = Some((1)))
      node24 <- Add.Add(node23,node9,broadcast = Some((1)),axis = Some((1)))
      node25 <- Reshape.Reshape(node24)
      node26 <- Transpose.Transpose(node25)
      node27 <- Reshape.Reshape(node26)
    } yield (node27)
}
