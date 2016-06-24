package io.fintrospect.formats.json

import com.google.gson.JsonElement

class GsonJsonFormatTest extends JsonFormatSpec[JsonElement, JsonElement](Gson.JsonFormat)

class GsonJsonResponseBuilderTest extends JsonResponseBuilderSpec(Gson)

