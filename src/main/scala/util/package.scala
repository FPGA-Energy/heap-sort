import chisel3.util.log2Ceil
import chisel3._

import java.io.{File, PrintWriter}
import scala.annotation.tailrec

package object util {

  def writeHexSeqToFile(seq: Seq[BigInt], fileName: String): Unit = {
    val writer = new PrintWriter(new File(fileName))
    writer.write(seq.map(_.toString(16)).mkString("\n"))
    writer.close()
  }

  def nextPow2(x: Int): Int = scala.math.pow(2, log2Ceil(x)).toInt


  def rising(x: Bool): Bool = !RegNext(x) && x
  def falling(x: Bool): Bool = RegNext(x) && !x


  implicit class BundleExpander[T <: Bundle](b: T) {
    def expand(assignments: T => Any*): Unit = assignments.foreach(f => f(b))
  }

  @tailrec
  def pipelinedReduce[T <: Data](x: Vec[T], groups: Int, fun: (T, T) => T): T = {
    if (x.length <= groups) reduce(x, fun)
    else pipelinedReduce(RegNext(
      VecInit(x.grouped(groups).map(g => reduce(g, fun)).toSeq)
    ), groups, fun)
  }

  def reduce[T <: Data](xs: Seq[T], fun: (T, T) => T): T = {
    xs match {
      case Seq(x) => x
      case Seq(x, y) => fun(x, y)
      case _ => reduce(xs.grouped(2).map(reduce(_, fun)).toSeq, fun)
    }
  }

  def roundAt(p: Int)(n: Double): Double = { val s = math pow (10, p); (math round n * s) / s }

}
