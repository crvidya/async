package scala.async.internal

import scala.tools.nsc.Global
import scala.tools.nsc.transform.TypingTransformers

object AsyncMacro {
  def apply(c: reflect.macros.Context, base: AsyncBase): AsyncMacro = {
    import language.reflectiveCalls
    val powerContext = c.asInstanceOf[c.type { val universe: Global; val callsiteTyper: universe.analyzer.Typer }]
    new AsyncMacro {
      val global: powerContext.universe.type   = powerContext.universe
      val callSiteTyper: global.analyzer.Typer = powerContext.callsiteTyper
      val macroApplication: global.Tree        = c.macroApplication.asInstanceOf[global.Tree]
      // This member is required by `AsyncTransform`:
      val asyncBase: AsyncBase                 = base
      // These members are required by `ExprBuilder`:
      val futureSystem: FutureSystem           = base.futureSystem
      val futureSystemOps: futureSystem.Ops {val universe: global.type} = futureSystem.mkOps(global)
    }
  }
}

private[async] trait AsyncMacro
  extends TypingTransformers
  with AnfTransform with TransformUtils with Lifter
  with ExprBuilder with AsyncTransform with AsyncAnalysis with LiveVariables {

  val global: Global
  val callSiteTyper: global.analyzer.Typer
  val macroApplication: global.Tree

  lazy val macroPos = macroApplication.pos.makeTransparent
  def atMacroPos(t: global.Tree) = global.atPos(macroPos)(t)

}
