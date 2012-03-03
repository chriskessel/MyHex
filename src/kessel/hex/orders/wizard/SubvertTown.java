package kessel.hex.orders.wizard;

import kessel.hex.domain.PopCenter;
import kessel.hex.domain.Wizard;

import java.util.Arrays;
import java.util.List;

/** Take control of a non-capitol Town or Town in the wizard's region. */
public class SubvertTown extends AbstractSubvertPopCenter
{
  private static final List<PopCenter.PopType> LEGAL_TARGETS = Arrays.asList( PopCenter.PopType.Hamlet, PopCenter.PopType.Town );

  public SubvertTown() { super(); } // GSON only
  public SubvertTown( Wizard wizard, PopCenter pop )
  {
    super( wizard, pop );
  }

  protected List<PopCenter.PopType> getSubvertablePopTypes()
  {
    return LEGAL_TARGETS;
  }
}
