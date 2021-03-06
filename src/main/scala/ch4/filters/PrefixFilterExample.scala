package ch4.filters

import org.apache.hadoop.hbase.client.{Get, Scan, ConnectionFactory}
import org.apache.hadoop.hbase.filter.PrefixFilter
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.{TableName, HBaseConfiguration}
import util.HBaseHelper
import util.ByteConverter._
import scala.collection.JavaConverters._

object PrefixFilterExample extends App {
  val conf = HBaseConfiguration.create()
  val helper = new HBaseHelper(conf)

  val tableName = TableName.valueOf("testtable")
  helper.dropTable(tableName)
  helper.createTable(tableName, List("colfam1", "colfam2"))
  println("Adding rows to table...")
  helper.fillTable(tableName, 1, 10, 10, -1, setTimestamp = false, random = false, List("colfam1"))

  val connection = ConnectionFactory.createConnection(conf)
  val table = connection.getTable(tableName)

  val filter = new PrefixFilter("row-1".toUTF8Byte)

  val scan = new Scan()
  scan.setFilter(filter)
  val scanner = table.getScanner(scan)

  println("Results of scan:")

  for {
    result <- scanner.asScala
    cell <- result.rawCells()
  } {
    println("Cell: " + cell + ", Value: " + Bytes.toString(cell.getValueArray, cell.getValueOffset, cell.getValueLength))
  }
  scanner.close()

  val get = new Get("row-5".toUTF8Byte)
  get.setFilter(filter)
  val result = table.get(get)

  println("Result of get: ")

  result.rawCells().foreach { cell =>
    println("Cell: " + cell + ", Value: " + Bytes.toString(cell.getValueArray, cell.getValueOffset, cell.getValueLength))
  }

  connection.close()
  table.close()
  helper.close()

}
