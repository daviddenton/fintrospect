<table class="code table table-bordered">
<tr>
  <td>Templating Library</td>
  <td>Template filename suffix</td>
  <td>Additional SBT deps</td>
  <td>Filter class</td>
</tr>
<tr>
  <td>Handlebars</td>
  <td>.hbs</td>
  <td>"com.gilt" %% "handlebars-scala" % "2.0.1"</td>
  <td>RenderHandlebarsView</td>
</tr>
<tr>
  <td>Mustache (v2.11 only)</td>
  <td>.mustache</td>
  <td>"com.github.spullara.mustache.java" % "compiler" % "0.9.1"<br/>"com.github.spullara.mustache.java" % "scala-extensions-2.11" % "0.9.1"</td>
  <td>RenderMustacheView</td>
</tr>
</table>
