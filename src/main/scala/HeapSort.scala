import HeapSort.State
import util.{falling, nextPow2, rising}
import util.Xilinx._
import chisel3._
import chisel3.util._
import firrtl.annotations.MemoryArrayInitAnnotation
import chisel3.experimental.{ChiselAnnotation, ChiselEnum, annotate}

import scala.io.Source


class HeapSort(params: Heap.Parameters, init: Seq[BigInt]) extends Module {
  import params._

  val io = IO(new Bundle {
    val iterationTick = Output(Bool())
  })


  val memory = SyncReadMem(16384, UInt(w.W))

  /*
  annotate(new ChiselAnnotation {
    override def toFirrtl = MemoryArrayInitAnnotation(memory.toTarget, init.padTo(16384, BigInt(0)))
  })
   */

  val heap = Module(new Heap(params))

  val stateReg = RegInit(State.InitLoop)
  val pointerReg = RegInit(0.U(log2Ceil(16384 + 1).W))

  val memOut = memory.read(pointerReg)

  val write = WireDefault(0.B)
  val wrData = WireDefault(heap.io.root)
  when(write) {
    memory.write(pointerReg, wrData)
  }

  heap.io.valid := 0.B
  heap.io.op := DontCare
  heap.io.newValue := DontCare

  io.iterationTick := 0.B

  switch(stateReg) {
    is(State.InitLoop) {
      wrData := pointerReg
      pointerReg := pointerReg + 1.U
      write := 1.B
      when(pointerReg === (init.length - 1).U) {
        stateReg := State.Setup
        pointerReg := 0.U
      } otherwise {
        stateReg := State.InitLoop
      }
    }
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
      io.iterationTick := 1.B

      pointerReg := 0.U
      stateReg := State.InitLoop
    }

  }


}

object HeapSort {

  object State extends ChiselEnum {
    val InitLoop, Setup, IssueInsert, WaitInsert, IssueRemove, WaitRemove, Done = Value
  }

}