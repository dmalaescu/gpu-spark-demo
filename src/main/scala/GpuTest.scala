import ai.rapids.cudf.Cuda

object GpuTest extends App {

  println(s"Gpu Device: ${Cuda.getDeviceCount}")
}
