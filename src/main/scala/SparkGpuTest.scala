import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{FloatType, IntegerType, StructField, StructType}

object SparkGpuTest extends App {

  lazy val schema =
    StructType(Array(
      StructField("vendor_id", FloatType),
      StructField("passenger_count", FloatType),
      StructField("trip_distance", FloatType),
      StructField("pickup_longitude", FloatType),
      StructField("pickup_latitude", FloatType),
      StructField("rate_code", FloatType),
      StructField("store_and_fwd", FloatType),
      StructField("dropoff_longitude", FloatType),
      StructField("dropoff_latitude", FloatType),
      StructField("fare_amount", FloatType),
      StructField("hour", FloatType),
      StructField("year", IntegerType),
      StructField("month", IntegerType),
      StructField("day", FloatType),
      StructField("day_of_week", FloatType),
      StructField("is_weekend", FloatType)
    ))

  val spark = SparkSession.builder()
    .appName("test gpu")
    .master("local[*]")
    .config("spark.plugins", "com.nvidia.spark.SQLPlugin")

    .config("spark.rapids.memory.pinnedPool.size", "1G")
    .config("spark.locality.wait","0s")
    .config("spark.rapids.sql.concurrentGpuTasks", 1)
    .config("spark.sql.files.maxPartitionBytes","512m")
//    .config("spark.rapids.sql.format.csv.enabled", "true")
//    .config("spark.rapids.sql.format.csv.read.enabled", "true")
//    .config("spark.rapids.sql.udfCompiler.enabled", "true")

//    .config("spark.executor.resource.gpu.amount","1")
//    .config("spark.task.resource.gpu.amount","1")

//    .config("spark.rapids.memory.gpu.allocFraction", 0.8)
//    .config("spark.sql.shuffle.partitions", 8)

    .getOrCreate()

//    GpuDeviceManager.getDeviceId() match {
//      case Some(v) => println(s"GPU-${v}")
//      case None => println("gpu not found")
//    }

  spark.sparkContext.setLogLevel("ERROR")

  import spark.implicits._

//  val df1 = spark.sparkContext.makeRDD(1 to 10000000, 6).toDF
//  val df2 = spark.sparkContext.makeRDD(1 to 10000000, 6).toDF
//  val join = df1.select( $"value" as "a").join(df2.select($"value" as "b"), $"a" === $"b")
//
//  join.explain(true)
//  join.count()


  //read from csv

  val df = spark
    .read
    .schema(schema)
    .parquet("/home/dma/Projects/gpu-spark-demo/src/main/resources/nyc_taxi")

//  df.write.mode(SaveMode.Overwrite)
//    .parquet("/home/dma/Projects/gpu-spark-demo/src/main/resources/nyc_taxi")

  println(s"Count is: ${df.count()}")
  df.explain(true)


  df.describe().show()


  val timeAmount = df
    .groupBy("year", "month", "day", "hour")
    .agg(avg("fare_amount"))

  timeAmount.explain(true)
  timeAmount.show()
}
