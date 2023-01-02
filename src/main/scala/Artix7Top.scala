
import util.Xilinx.MMCME2_ADV
import chisel3._
import chisel3.util._

import scala.io.Source

class Artix7Top(params: Heap.Parameters, init: Seq[BigInt], frequency: Double) extends Module {

  val io = IO(new Bundle {
    val leds = Output(UInt(2.W))
    val rgb = Output(UInt(3.W))
  })

  withClock(MMCME2_ADV(clock,reset, 12.0 -> frequency)) {

    val experiment = Module(new ExperimentTop(params, init))
    io.leds := Fill(3, experiment.io.blink)
    io.rgb := Fill(3, !experiment.io.blink)

  }

}

object Artix7Top extends App {
  val defaultK = 4
  val defaultTestFile = "src/test-files/16K-sorted.txt"

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

  emitVerilog(new Artix7Top(Heap.Parameters(16384, k, w), testSeq.padTo(16384, BigInt(0)), frequencies(k)), Array("--target-dir", targetDir))
}