package uk.co.elder.app.charting

object MathExtra {
  def percentDifference(a: BigDecimal, b: BigDecimal) : BigDecimal = ((b - a) / a) * 100
}