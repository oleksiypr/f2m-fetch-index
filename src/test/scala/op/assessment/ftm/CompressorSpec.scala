package op.assessment.ftm

import org.scalatest.FunSuite

class CompressorSpec  extends FunSuite with Compressor {

  test("compress  empty") {
    assert(compress(Array.empty[String]).isEmpty)
  }

  test("compress single") {
    assert(compress(Array("A")) === Array(Single("A")))
  }

  test("compress repeated") {
    assert(compress(Array("A", "A", "A")) ===
      Array(Repeat(3, "A"))
    )
  }

  test("compress many single") {
    assert(compress(Array("A", "B", "C")) ===
      Array(Single("A"), Single("B"), Single("C"))
    )
  }

  test("compress  mixed") {
    assert(compress(Array("A", "A", "A", "B", "C", "C")) ===
      Array(Repeat(3, "A"), Single("B"), Repeat(2, "C"))
    )
  }

  test("decompress empty") {
    assert(decompress(Array.empty[Compressed[String]]).isEmpty)
  }

  test("decompress single") {
    assert(decompress(Array(Single("A"))) === Array("A"))
  }

  test("decompress repeated") {
    assert(decompress(Array(Repeat(3, "A"))) ===
      Array("A", "A", "A"))
  }

  test("decompress many single") {
    assert(decompress(
      Array(Single("A"), Single("B"), Single("C"))) ===
      Array("A", "B", "C")
    )
  }

  test("decompress  mixed") {
    assert(decompress(
      Array(Repeat(3, "A"), Single("B"), Repeat(2, "C"))) ===
      Array("A", "A", "A", "B", "C", "C")
    )
  }

  test("compress-decompress") {
    val items =  Array("A", "A", "A", "B", "C", "C")
    assert(
      (compress[String] _ andThen decompress)(items) === items
    )
  }
}
