package io.fintrospect.formats

import com.google.gson.JsonElement

class GsonJsonFormatTest extends JsonFormatSpec[JsonElement, JsonElement](Gson)

class GsonJsonResponseBuilderTest extends JsonResponseBuilderSpec(Gson)

