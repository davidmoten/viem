package viem

import scala.io._
import scala.xml.XML

object MaritimeExample {

	def main(args: Array[String]) {
		Source.fromFile("/home/dave/Downloads/fixes.xml", "UTF-8").getLines.foreach(line => {
			val xml = XML.loadString(line)
			println(xml)
			println (xml \ "@agent" + (xml \ "@time").toString)
			Unit
		})
	}
	
}

class MaritimeExample {
	
	
}