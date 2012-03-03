package kessel.hex.admin;

import kessel.hex.map.Terrain;
import kessel.hex.util.HexCalculator;
import kessel.hex.util.Tuple;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

/** A utility that knows how to paint various orders. */
public class PaintOrderUtil
{
  public static void paintArmyMove( Graphics g, PlayerDetailsPane detailsPane, List<Tuple> moves )
  {
    Map<Terrain, Integer> costs = detailsPane.getPlayer().getKingdom().getMovementCosts();
    int cost = 0;
    Font originalFont = g.getFont();
    g.setColor( detailsPane.getPlayerColors().get( detailsPane.getPlayer().getName() ) );
    Font newFont = g.getFont().deriveFont( Font.PLAIN, g.getFont().getSize() + 3 );
    g.setFont( newFont );

    // Paint the running cost of each hex moved to.
    for ( Tuple move : moves )
    {
      cost += costs.get( detailsPane._gameDisplay.getLocationAtHex( move ).getTerrain() );
      HexCalculator cellMetrics = detailsPane._gameDisplay.getCellMetrics();
      Tuple gridCenter = cellMetrics.getPixelCenterByGrid( move.x, move.y );
      String costStr = String.valueOf( cost );
      Rectangle2D fontBox = g.getFontMetrics().getStringBounds( costStr, g );
      g.drawString( costStr, gridCenter.x - (int) (fontBox.getWidth() / 2), gridCenter.y + (int) (fontBox.getHeight() / 2) );
    }
    g.setFont( originalFont );
  }

  public static void paintConnectHexes( Graphics g, PlayerDetailsPane detailsPane, Tuple src, Tuple dest )
  {
    g.setColor( detailsPane.getPlayerColors().get( detailsPane.getPlayer().getName() ) );
    HexCalculator cellMetrics = detailsPane._gameDisplay.getCellMetrics();
    Tuple srcCenter = cellMetrics.getPixelCenterByGrid( src );
    Tuple destCenter = cellMetrics.getPixelCenterByGrid( dest );
    g.drawLine( srcCenter.x, srcCenter.y, destCenter.x, destCenter.y );
  }
}
