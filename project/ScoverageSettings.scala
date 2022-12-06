import scoverage.ScoverageKeys
  
object ScoverageSettings {
  def apply() = Seq(
    ScoverageKeys.coverageExcludedPackages := Seq(
      "<empty>",
      """uk\.gov\.hmrc\.BuildInfo""" ,
      """.*\.Routes""" ,
      """.*\.RoutesPrefix""" ,
      """.*Filters?""" ,
      """MicroserviceAuditConnector""" ,
      """Module""" ,
      """GraphiteStartUp""" ,
      """.*\.Reverse[^.]*""",
    ).mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 82,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}
