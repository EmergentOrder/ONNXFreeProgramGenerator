package org.emergentorder.onnx

import cats.free.{ Free, FreeApplicative } 

      import UnionType._
      import scala.reflect.ClassTag
import spire.implicits._
import spire.math.UByte
import spire.math.UShort
import spire.math.Complex
import spire.algebra.Field
import spire.math.Numeric
import singleton.ops._
import scala.language.higherKinds

trait Super_resolutionFree {
  val ConvFree: ConvFree
  val AddFree: AddFree
  val ReluFree: ReluFree
  val ReshapeFree: ReshapeFree
  val TransposeFree: TransposeFree
  val dataSource: DataSourceFree
  def program[T : (UNil TypeOr Float16 TypeOr Float TypeOr Double)#check:Numeric:ClassTag:Field, J <: XInt] = 
    for {
      node1 <- dataSource.inputDataFree[T,J]
      node8 <-  dataSource.getParamsFree[T,J]("8")
      node4 <-  dataSource.getParamsFree[T,J]("4")
      node9 <-  dataSource.getParamsFree[T,J]("9")
      node5 <-  dataSource.getParamsFree[T,J]("5")
      node6 <-  dataSource.getParamsFree[T,J]("6")
      node2 <-  dataSource.getParamsFree[T,J]("2")
      node7 <-  dataSource.getParamsFree[T,J]("7")
      node3 <-  dataSource.getParamsFree[T,J]("3")
      node11 <- ConvFree.Conv1Free("11", node1, "1",node2, "2",kernel_shape = Some((Seq(5,5))),strides = Some((Seq(1,1))),pads = Some((Seq(2,2,2,2))),dilations = Some((Seq(1,1))),group = Some((1)))
      node12 <- AddFree.Add1Free("12", node11, "11",node3, "3",broadcast = Some((1)),axis = Some((1)))
      node13 <- ReluFree.Relu1Free("13", node12, "12")
      node15 <- ConvFree.Conv1Free("15", node13, "13",node4, "4",kernel_shape = Some((Seq(3,3))),strides = Some((Seq(1,1))),pads = Some((Seq(1,1,1,1))),dilations = Some((Seq(1,1))),group = Some((1)))
      node16 <- AddFree.Add1Free("16", node15, "15",node5, "5",broadcast = Some((1)),axis = Some((1)))
      node17 <- ReluFree.Relu1Free("17", node16, "16")
      node19 <- ConvFree.Conv1Free("19", node17, "17",node6, "6",kernel_shape = Some((Seq(3,3))),strides = Some((Seq(1,1))),pads = Some((Seq(1,1,1,1))),dilations = Some((Seq(1,1))),group = Some((1)))
      node20 <- AddFree.Add1Free("20", node19, "19",node7, "7",broadcast = Some((1)),axis = Some((1)))
      node21 <- ReluFree.Relu1Free("21", node20, "20")
      node23 <- ConvFree.Conv1Free("23", node21, "21",node8, "8",kernel_shape = Some((Seq(3,3))),strides = Some((Seq(1,1))),pads = Some((Seq(1,1,1,1))),dilations = Some((Seq(1,1))),group = Some((1)))
      node24 <- AddFree.Add1Free("24", node23, "23",node9, "9",broadcast = Some((1)),axis = Some((1)))
      node25 <- ReshapeFree.Reshape1Free("25", node24, "24",shape = Some((Seq(1,1,3,3,224,224))))
      node26 <- TransposeFree.Transpose1Free("26", node25, "25",perm = Some((Seq(0,1,4,2,5,3))))
      node27 <- ReshapeFree.Reshape1Free("27", node26, "26",shape = Some((Seq(1,1,672,672))))
    } yield (node27)
}
