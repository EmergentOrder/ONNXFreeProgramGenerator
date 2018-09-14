package org.emergentorder.onnx

import org.emergentorder.onnx.UnionType._
import scala.reflect.ClassTag
import spire.implicits._
import spire.math.UByte
import spire.math.UShort
import spire.math.Complex
import spire.algebra.Field
import spire.math.Numeric
import singleton.ops._
import scala.language.higherKinds

trait Super_resolution {
  val Conv: Conv
  val Add: Add
  val Relu: Relu
  val Reshape: Reshape
  val Transpose: Transpose
  val dataSource: DataSource
  def program[T : (UNil TypeOr Float16 TypeOr Float TypeOr Double)#check:Numeric:ClassTag:Field, J <: XInt]: List[Tensor[T,J]]  = 
    for {
      node1 <- List(dataSource.inputData[T,J])
      node8 <- List( dataSource.getParams[T,J]("8"))
      node4 <- List( dataSource.getParams[T,J]("4"))
      node9 <- List( dataSource.getParams[T,J]("9"))
      node5 <- List( dataSource.getParams[T,J]("5"))
      node6 <- List( dataSource.getParams[T,J]("6"))
      node2 <- List( dataSource.getParams[T,J]("2"))
      node7 <- List( dataSource.getParams[T,J]("7"))
      node3 <- List( dataSource.getParams[T,J]("3"))
      node11 <- List(Conv.Conv1("11", node1, "1",node2, "2",kernel_shape = Some((Seq(5,5))),strides = Some((Seq(1,1))),pads = Some((Seq(2,2,2,2))),dilations = Some((Seq(1,1))),group = Some((1))))
      node12 <- List(Add.Add1("12", node11, "11",node3, "3",broadcast = Some((1)),axis = Some((1))))
      node13 <- List(Relu.Relu1("13", node12, "12"))
      node15 <- List(Conv.Conv1("15", node13, "13",node4, "4",kernel_shape = Some((Seq(3,3))),strides = Some((Seq(1,1))),pads = Some((Seq(1,1,1,1))),dilations = Some((Seq(1,1))),group = Some((1))))
      node16 <- List(Add.Add1("16", node15, "15",node5, "5",broadcast = Some((1)),axis = Some((1))))
      node17 <- List(Relu.Relu1("17", node16, "16"))
      node19 <- List(Conv.Conv1("19", node17, "17",node6, "6",kernel_shape = Some((Seq(3,3))),strides = Some((Seq(1,1))),pads = Some((Seq(1,1,1,1))),dilations = Some((Seq(1,1))),group = Some((1))))
      node20 <- List(Add.Add1("20", node19, "19",node7, "7",broadcast = Some((1)),axis = Some((1))))
      node21 <- List(Relu.Relu1("21", node20, "20"))
      node23 <- List(Conv.Conv1("23", node21, "21",node8, "8",kernel_shape = Some((Seq(3,3))),strides = Some((Seq(1,1))),pads = Some((Seq(1,1,1,1))),dilations = Some((Seq(1,1))),group = Some((1))))
      node24 <- List(Add.Add1("24", node23, "23",node9, "9",broadcast = Some((1)),axis = Some((1))))
      node25 <- List(Reshape.Reshape1("25", node24, "24",shape = Some((Seq(1,1,3,3,224,224)))))
      node26 <- List(Transpose.Transpose1("26", node25, "25",perm = Some((Seq(0,1,4,2,5,3)))))
      node27 <- List(Reshape.Reshape1("27", node26, "26",shape = Some((Seq(1,1,672,672)))))
    } yield (node27)
}
