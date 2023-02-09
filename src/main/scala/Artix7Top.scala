
import util.Xilinx.MMCME2_ADV
import chisel3._
import chisel3.util._

import scala.io.Source

class Artix7Top(params: Heap.Parameters, init: Seq[BigInt], frequency: Double)(lowCycles: Int, highCycles: Int) extends Module {

  val io = IO(new Bundle {
    val leds = Output(UInt(2.W))
    val rgb = Output(UInt(3.W))
  })

  println(lowCycles -> highCycles)

  withClock(MMCME2_ADV(clock,reset, 12.0 -> frequency)) {

    val experiment = Module(new ExperimentTop(params, init)(lowCycles, highCycles))
    io.leds := Fill(3, experiment.io.blink)
    io.rgb := Fill(3, !experiment.io.blink)

  }

}

object Artix7Top extends App {
  val defaultK = 2
  val defaultTestFile = "src/test-files/10K-sorted.txt"

  val k = if (args.contains("-k")) args(args.indexOf("-k") + 1).toInt else defaultK
  val w = if (args.contains("-w")) args(args.indexOf("-w") + 1).toInt else 32
  val targetDir = if (args.contains("--target-dir")) args(args.indexOf("--target-dir") + 1) else "build"
  val testSeq = {
    val testFile = if (args.contains("--test-file")) args(args.indexOf("--test-file") + 1) else defaultTestFile
    val source = Source.fromFile(testFile)
    source.getLines().map(BigInt(_, 16)).toArray
  }

  // target frequencies for different values of k
  val frequencies = Map(
    2 -> 140,
    4 -> 140,
    8 -> 140,
    16 -> 138,
    32 -> 138,
    64 -> 125
  )

  def fun(repsPerSec: Int) = Seq.tabulate(7) { i =>
    val n = 4096 + i*2048
    n -> ((repsPerSec * 5 * (4096f/n)).toInt, (repsPerSec * (4096f/n)).toInt)
  }.toMap

  val lowHighs = Map(
    2 -> fun(125),
    4 -> fun(200),
    8 -> fun(250),
    16 -> fun(310),
    32 -> fun(340),
    64 -> fun(360)
  )

  println("k, n, low, high, frequency")
  println(lowHighs.flatMap { case (k, map) =>
    map.map { case (n, (low, high)) => s"$k, $n, $low, $high, ${frequencies(k)*1000000}" }
  }.mkString("\n"))

  val (lowCycles, highCycles) = lowHighs.apply(k).apply(testSeq.length)

  emitVerilog(new Artix7Top(Heap.Parameters(16384, k, w), testSeq, frequencies(k))(lowCycles, highCycles), Array("--target-dir", targetDir))
}