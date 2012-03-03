package kessel.hex.domain;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import static org.junit.Assert.assertEquals;

public class FigureTest<X extends Figure> extends GameItemTest<X>
{
  private static final Logger LOG = Logger.getLogger( FigureTest.class );

  static
  {
    LOG.addAppender( new ConsoleAppender( new PatternLayout( PatternLayout.TTCC_CONVERSION_PATTERN ) ) );
  }

  public void doEqualsTest( X a, X b ) throws Exception
  {
    super.doEqualsTest( a, b );
    assertEquals( a.getLevel(), b.getLevel() );
    assertEquals( a.getBase().getId(), b._jsonBaseId );
    assertEquals( a.getRange(), b.getRange() );
  }
}
