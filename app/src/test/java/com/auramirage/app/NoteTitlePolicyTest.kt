package com.auramirage.app
import org.junit.Assert.assertEquals
import org.junit.Test
class NoteTitlePolicyTest{@Test fun firstLine(){assertEquals("Minha anotação",NoteTitlePolicy.normalize("","Minha anotação\nmais texto"))}@Test fun explicit(){assertEquals("Compras",NoteTitlePolicy.normalize(" Compras ","arroz"))}@Test fun fallback(){assertEquals("Sem título",NoteTitlePolicy.normalize("",""))}}
