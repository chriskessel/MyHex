package kessel.hex.orders.wizard;

import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;

import java.util.Arrays;
import java.util.List;

/** Take control of a non-capitol City, Town, or Hamlet in the wizard's region. */
public class SubvertCity extends AbstractSubvertPopCenter
{
  private static final List<PopCenter.PopType> LEGAL_TARGETS =
    Arrays.asList( PopCenter.PopType.Hamlet, PopCenter.PopType.Town, PopCenter.PopType.City );

  public SubvertCity() { super(); } // GSON only
  public SubvertCity( Wizard wizard, PopCenter pop )
  {
    super( wizard, pop );
  }

  protected List<PopCenter.PopType> getSubvertablePopTypes()
  {
    return LEGAL_TARGETS;
  }
}
