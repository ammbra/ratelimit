package org.acme.example;

import io.quarkus.test.junit.NativeImageTest;

@NativeImageTest
public class NativeGreeterResourceIT extends GreeterResourceTest {

    // Execute the same tests but in native mode.
}