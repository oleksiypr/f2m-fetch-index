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
}
