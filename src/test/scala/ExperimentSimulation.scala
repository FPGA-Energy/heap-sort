
import chisel3._
import chiseltest._
import org.scalatest.flatspec.AnyFlatSpec
import treadle.WriteVcdAnnotation

import scala.io.Source

class ExperimentSimulation extends AnyFlatSpec with ChiselScalatestTester {

  "Experiment" should "run" in {
    val k = 4
    val w = 32
    val testSeq = {
      val testFile = "src/test-files/4K-sorted.txt"
      val source = Source.fromFile(testFile)
      source.getLines().map(BigInt(_, 16)).toArray
    }
    test(new ExperimentTop(Heap.Parameters(16384, k, w),testSeq)).withAnnotations(Seq(WriteVcdAnnotation)) { dut =>
      dut.clock.setTimeout(0)
      while(!dut.io.blink.peek.litToBoolean) dut.clock.step()
    }
  }

}
