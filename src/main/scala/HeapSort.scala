import HeapSort.State
import util.{falling, nextPow2, rising}
import util.Xilinx._
import chisel3._
import chisel3.util._
import firrtl.annotations.MemoryArrayInitAnnotation
import chisel3.experimental.{ChiselAnnotation, ChiselEnum, annotate}

import scala.io.Source


class HeapSort(params: Heap.Parameters, init: Seq[BigInt], lowCycles: Int = 500, highCycles: Int = 15, frequency: Double) extends Module {
  import params._

  val io = IO(new Bundle {
    val leds = Output(UInt(2.W))
    val rgb = Output(UInt(3.W))
  })


  withClock(MMCME2_ADV(clock,reset, 12.0 -> frequency)) {

    val memory = SyncReadMem(16384, UInt(w.W))

    annotate(new ChiselAnnotation {
      override def toFirrtl = MemoryArrayInitAnnotation(memory.toTarget, init.padTo(16384, BigInt(0)))
    })

    val heap = Module(new Heap(params))

    val stateReg = RegInit(State.Setup)
    val pointerReg = RegInit(0.U(log2Ceil(16384 + 1).W))

    val memOut = memory.read(pointerReg)

    val write = WireDefault(0.B)
    when(write) {
      memory.write(pointerReg, heap.io.root)
    }

    heap.io.valid := 0.B
    heap.io.op := DontCare
    heap.io.newValue := DontCare

    val runCounter = RegInit(0.U(log2Ceil(lowCycles).W))
    val blinkReg = RegInit(0.B)

    io.leds := Fill(2, blinkReg)
    io.rgb := Fill(3, !blinkReg)

    switch(stateReg) {
      is(State.Setup) {
        stateReg := State.IssueInsert
        pointerReg := pointerReg + 1.U
      }
      is(State.IssueInsert) {
        heap.io.op := Heap.Operation.Insert
        heap.io.newValue := memOut
        heap.io.valid := 1.B
        pointerReg := pointerReg + 1.U
        stateReg := State.WaitInsert
      }
      is(State.WaitInsert) {
        stateReg := Mux(heap.io.ready, Mux(pointerReg === init.length.U, State.IssueRemove, State.IssueInsert), State.WaitInsert)
        when(heap.io.ready && pointerReg === init.length.U) {
          pointerReg := (init.length - 1).U
        }
      }
      is(State.IssueRemove) {
        heap.io.op := Heap.Operation.RemoveRoot
        heap.io.valid := 1.B
        write := 1.B
        pointerReg := pointerReg - 1.U
        stateReg := Mux(pointerReg === 0.U, State.Done, State.WaitRemove)
      }
      is(State.WaitRemove) {
        stateReg := Mux(heap.io.ready, State.IssueRemove, State.WaitRemove)
      }
      is(State.Done) {
        runCounter := runCounter + 1.U

        when((runCounter === lowCycles.U && !blinkReg) || (runCounter === highCycles.U && blinkReg)) {
          runCounter := 0.U
          blinkReg := !blinkReg
        }

        pointerReg := 0.U
        stateReg := State.Setup
      }

    }
  }

}

object HeapSort {

  object State extends ChiselEnum {
    val Setup, IssueInsert, WaitInsert, IssueRemove, WaitRemove, Done = Value
  }

  def main(args: Array[String]) = {

    val defaultK = 4
    val defaultTestFile = "src/test-files/16K-sorted.txt"

    val k = if(args.contains("-k")) args(args.indexOf("-k") + 1).toInt else defaultK
    val w = if(args.contains("-w")) args(args.indexOf("-w") + 1).toInt else 32
    val targetDir = if(args.contains("--target-dir")) args(args.indexOf("--target-dir") + 1) else "build"
    val testSeq = {
      val testFile = if(args.contains("--test-file")) args(args.indexOf("--test-file") + 1) else defaultTestFile
      val source = Source.fromFile(testFile)
      source.getLines().map(BigInt(_, 16)).toArray
    }

    val frequencies = Map(
      2 -> 140,
      4 -> 140,
      8 -> 140,
      16 -> 138,
      32 -> 138,
      64 -> 125
    )
    val lowCycles = 500
    val highCycles = 15

    emitVerilog(new HeapSort(Heap.Parameters(16384, k, w), testSeq.padTo(16384, BigInt(0)), lowCycles, highCycles, frequencies(k)), Array("--target-dir",targetDir))
  }

}