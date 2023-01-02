
import util.Xilinx.MMCME2_ADV
import chisel3._
import chisel3.util.{Fill, log2Ceil}

class ExperimentTop(params: Heap.Parameters, init: Seq[BigInt]) extends Module {

  val lowCycles = 500
  val highCycles = 15


  val io = IO(new Bundle {
    val blink = Output(Bool())
  })

  val heapSort = Module(new HeapSort(params, init))

  val runCounter = RegInit(0.U(log2Ceil(math.max(lowCycles, highCycles)).W))
  val blinkReg = RegInit(0.B)
  io.blink := blinkReg

  when(heapSort.io.iterationTick) {
    runCounter := runCounter - 1.U
    when(runCounter === 0.U) {
      runCounter := Mux(blinkReg, lowCycles.U, highCycles.U)
      blinkReg := !blinkReg
    }
  }



}
