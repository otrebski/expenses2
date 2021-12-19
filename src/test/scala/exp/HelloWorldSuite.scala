package exp

import munit.CatsEffectSuite

class HelloWorldSuite extends CatsEffectSuite {

  test("test hello world says hi") {
    assertEquals("it", "it")
  }
}
