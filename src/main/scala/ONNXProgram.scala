package org.emergentorder.onnx

import freestyle.free._
import cats.free.{ Free, FreeApplicative } 
import freestyle.free.implicits._
import scala.reflect.ClassTag
import scala.language.higherKinds

@module trait Application {
  val Conv: Conv
  val Add: Add
  val Relu: Relu
  val Reshape: Reshape
  val Transpose: Transpose
  val dataSource: DataSource
  import cats.implicits._
  def program[VV:spire.math.Numeric:ClassTag] = 
    for {
      node1 <- dataSource.inputData[VV]
      node8 <- dataSource.getParams[VV]("8")
      node4 <- dataSource.getParams[VV]("4")
      node9 <- dataSource.getParams[VV]("9")
      node5 <- dataSource.getParams[VV]("5")
      node6 <- dataSource.getParams[VV]("6")
      node2 <- dataSource.getParams[VV]("2")
      node7 <- dataSource.getParams[VV]("7")
      node3 <- dataSource.getParams[VV]("3")
      node11 <- Conv.Conv1[VV]("11", node1, "1",node2, "2",kernel_shape = Some((Array("5","5"))),strides = Some((Array("1","1"))),pads = Some((Array("2","2","2","2"))),dilations = Some((Array("1","1"))))
      node12 <- Add.Add1[VV]("12", node1, "1",node2, "2")
      node13 <- Relu.Relu1[VV]("13", node1, "1")
      node15 <- Conv.Conv1[VV]("15", node1, "1",node2, "2",kernel_shape = Some((Array("3","3"))),strides = Some((Array("1","1"))),pads = Some((Array("1","1","1","1"))),dilations = Some((Array("1","1"))))
      node16 <- Add.Add1[VV]("16", node1, "1",node2, "2")
      node17 <- Relu.Relu1[VV]("17", node1, "1")
      node19 <- Conv.Conv1[VV]("19", node1, "1",node2, "2",kernel_shape = Some((Array("3","3"))),strides = Some((Array("1","1"))),pads = Some((Array("1","1","1","1"))),dilations = Some((Array("1","1"))))
      node20 <- Add.Add1[VV]("20", node1, "1",node2, "2")
      node21 <- Relu.Relu1[VV]("21", node1, "1")
      node23 <- Conv.Conv1[VV]("23", node1, "1",node2, "2",kernel_shape = Some((Array("3","3"))),strides = Some((Array("1","1"))),pads = Some((Array("1","1","1","1"))),dilations = Some((Array("1","1"))))
      node24 <- Add.Add1[VV]("24", node1, "1",node2, "2")
      node25 <- Reshape.Reshape1[VV]("25", node1, "1",shape = Some((Array("1","1","3","3","224","224"))))
      node26 <- Transpose.Transpose1[VV]("26", node1, "1",perm = Some((Array("0","1","4","2","5","3"))))
      node27 <- Reshape.Reshape1[VV]("27", node1, "1",shape = Some((Array("1","1","672","672"))))
    } yield (node27)
}
