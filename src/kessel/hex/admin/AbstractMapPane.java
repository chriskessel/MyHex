package kessel.hex.admin;

import kessel.hex.domain.GameItem;
import kessel.hex.domain.PopCenter;
import kessel.hex.map.Location;
import kessel.hex.map.MapCreator;
import kessel.hex.map.Region;
import kessel.hex.util.ColorPicker;
import kessel.hex.util.HexCalculator;
import kessel.hex.util.Tuple;
import sun.awt.image.ToolkitImage;

import javax.swing.*;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import static kessel.hex.admin.AbstractGameDisplay.CELL_METRICS;

/** Bulk of the functionality for drawing the game map. */
abstract class AbstractMapPane extends JPanel implements MapCreator.MapCreationListener
{
  static final List<Color> REGION_COLORS = new ArrayList<>();

  static
  {
    for ( int i = 1; i <= 3; i++ )
    {
      // The darker colors of the combo colors.
      REGION_COLORS.add( new Color( 65 + (60 * i), 0, 0 ) );
      REGION_COLORS.add( new Color( 0, 0, 65 + (60 * i) ) );
      REGION_COLORS.add( new Color( 0, 60 + (60 * i), 0 ) );

      // Shades of grey.
      REGION_COLORS.add( new Color( 40 + (60 * i), 40 + (60 * i), 40 + (60 * i) ) );
    }
  }

  static final Color UNOWNED_MAIN_COLOR = new Color( 57, 57, 57 );
  static final Color UNOWNED_SECONDARY_COLOR = new Color( 255, 255, 255 );


  // Images for the terrain and pops.
  BufferedImage _tree, _mountain, _desert, _plain;
  BufferedImage _city, _town, _hamlet;
  BufferedImage _ownedCity, _ownedTown, _ownedHamlet;
  BufferedImage _noGameYet;
  Image _mapImage = null;

  final Map<String, Color> _playerColors = new HashMap<>();

  private final AbstractGameDisplay _gameDisplay;

  protected AbstractMapPane( AbstractGameDisplay gameDisplay )
  {
    _gameDisplay = gameDisplay;
    loadNoGameYetImage();
    loadTerrainImages();
  }

  private void loadNoGameYetImage()
  {
    ImageIcon iic = new ImageIcon( "./design/dragon_on_a_leash.png" );
    _noGameYet = ((ToolkitImage) iic.getImage()).getBufferedImage();
  }

  private void loadTerrainImages()
  {
    ImageIcon iic = new ImageIcon( "./design/Forest.png" );
    _tree = ((ToolkitImage) iic.getImage()).getBufferedImage();
    iic = new ImageIcon( "./design/Mountain.png" );
    _mountain = ((ToolkitImage) iic.getImage()).getBufferedImage();
    iic = new ImageIcon( "./design/Desert.png" );
    _desert = ((ToolkitImage) iic.getImage()).getBufferedImage();
    iic = new ImageIcon( "./design/Plain.png" );
    _plain = ((ToolkitImage) iic.getImage()).getBufferedImage();

    iic = new ImageIcon( "./design/City.png" );
    _city = ((ToolkitImage) iic.getImage()).getBufferedImage();
    iic = new ImageIcon( "./design/Town.png" );
    _town = ((ToolkitImage) iic.getImage()).getBufferedImage();
    iic = new ImageIcon( "./design/Hamlet.png" );
    _hamlet = ((ToolkitImage) iic.getImage()).getBufferedImage();

    iic = new ImageIcon( "./design/OwnedCity.png" );
    _ownedCity = ((ToolkitImage) iic.getImage()).getBufferedImage();
    iic = new ImageIcon( "./design/OwnedTown.png" );
    _ownedTown = ((ToolkitImage) iic.getImage()).getBufferedImage();
    iic = new ImageIcon( "./design/OwnedHamlet.png" );
    _ownedHamlet = ((ToolkitImage) iic.getImage()).getBufferedImage();
  }

  public Dimension getPreferredSize()
  {
    if ( _gameDisplay.isLoaded() )
    {
      int mapWidth = _gameDisplay.getMapWidth();
      int mapHeight = _gameDisplay.getMapHeight();
      int pixelWidth = ((CELL_METRICS._radius * 3 * mapWidth)) / 2 + (CELL_METRICS._width - CELL_METRICS._side);
      int pixelHeight = (CELL_METRICS._height * mapHeight) + (CELL_METRICS._height / 2);
      return new Dimension( pixelWidth, pixelHeight );
    }
    else
    {
      return new Dimension( _noGameYet.getWidth(), _noGameYet.getHeight() );
    }
  }

  public void init()
  {
    _mapImage = null;
  }

  public void mapChanged()
  {
    repaint();
  }

  @SuppressWarnings({ "AssignmentToMethodParameter" })
  public void paint( Graphics g )
  {
    super.paint( g );
    if ( canPaint() )
    {
      if ( _mapImage == null )
      {
        Graphics origGraphics = g;
        Dimension size = getPreferredSize();
        _mapImage = this.createImage( size.width + 1, size.height + 1 );
        g = _mapImage.getGraphics();

        setupPlayerColors();
        paintLocationsAndTerrain( g );
        paintStuff( g );
        g = origGraphics;
      }
      g.drawImage( _mapImage, 0, 0, this );
      paintExtras( g );
      drawSelectedHex( (Graphics2D) g );
    }
    else
    {
      paintNoGameYet( g );
    }
  }

  private void paintNoGameYet( Graphics g )
  {
    g.drawImage( _noGameYet, 0, 0, null );
  }

  protected void setupPlayerColors()
  {
    if ( _playerColors.isEmpty() )
    {
      List<String> playerNames = getPlayerNames();
      List<Color> colors = ColorPicker.chooseDistinguishableColors( playerNames.size() );
      int i = 0;
      for ( String playerName : playerNames )
      {
        _playerColors.put( playerName, colors.get( i ) );
        i++;
      }
    }
  }

  protected abstract List<String> getPlayerNames();

  protected abstract boolean canPaint();

  protected void paintLocationsAndTerrain( Graphics g )
  {
    Map<String, Color> regionColors = assignRegionColors( getRegionNames() );
    Map<Tuple, Location> locations = getLocations();
    for ( Location location : locations.values() )
    {
      // Define the outline of the hex to draw.
      Tuple pixelCenter = CELL_METRICS.getPixelCenterByGrid( location.getCoord().x, location.getCoord().y );
      int[][] regionCorners = CELL_METRICS.computePixelCorners( location.getCoord().x, location.getCoord().y, 0 );
      Polygon hexBoundaries = new Polygon( regionCorners[0], regionCorners[1], AbstractGameDisplay.HEX_SIDES );

      // Figure out the terrain color and image.
      Image terrainImage = null;
      Color terrainColor = Color.lightGray.brighter();
      switch ( location.getTerrain() )
      {
        case Forest:
          terrainImage = _tree;
          terrainColor = new Color( 185, 255, 190 );
          break;
        case Mountain:
          terrainImage = _mountain;
          terrainColor = new Color( 240, 230, 180 );
          break;
        case Desert:
          terrainImage = _desert;
          terrainColor = new Color( 255, 250, 165 );
          break;
        case Plain:
          terrainImage = _plain;
          terrainColor = new Color( 235, 255, 180 );
          break;
        case Undefined:
          break;
      }

      // Draw the terrain color.
      Region region = location.getRegion();
      g.setColor( terrainColor );
      g.fillPolygon( hexBoundaries );

      // Draw the hex border indicating the region.
      if ( !region.equals( Region.UNKNOWN_REGION ) )
      {
        List<Polygon> borders = getEdgeBordersToFill( location );
        g.setColor( regionColors.get( region.getName() ) );
        for ( Polygon border : borders )
        {
          g.fillPolygon( border );
        }
      }

      // Draw the terrain image.
      if ( terrainImage != null )
      {
        int xScale = CELL_METRICS._width;
        int yScale = CELL_METRICS._height;
        int xOffset = CELL_METRICS._radius;
        int yOffset = CELL_METRICS._height / 2;
        g.setClip( hexBoundaries );
        g.drawImage( terrainImage, pixelCenter.x - xOffset, pixelCenter.y - yOffset, xScale, yScale, null );
        g.setClip( null );
      }

      // Draw the hex itself.
      g.setColor( Color.BLACK );
      g.drawPolygon( regionCorners[0], regionCorners[1], AbstractGameDisplay.HEX_SIDES );
    }
  }

  // Highlight the selected hex.
  private void drawSelectedHex( Graphics2D g2 )
  {
    if ( _gameDisplay.getSelectedHex() != null )
    {
      Stroke normalStroke = g2.getStroke();
      g2.setStroke( new BasicStroke( 3 ) );
      Tuple hex = _gameDisplay.getSelectedHex();
      int[][] regionCorners = CELL_METRICS.computePixelCorners( hex.x, hex.y, 0 );
      g2.setColor( Color.RED );
      g2.drawPolygon( regionCorners[0], regionCorners[1], AbstractGameDisplay.HEX_SIDES );
      g2.setStroke( normalStroke );
    }
  }

  protected abstract Map<Tuple, Location> getLocations();

  protected void paintStuff( Graphics g )
  {
    for ( GameItem item : getGameItems() )
    {
      // Display the pop center.
      if ( item instanceof PopCenter )
      {
        PopCenter pop = (PopCenter) item;
        Location location = pop.getLocation();
        Tuple pixelCenter = CELL_METRICS.getPixelCenterByGrid( location.getCoord().x, location.getCoord().y );

        BufferedImage popImage = null;
        int xScale = 0, yScale = 0;
        int xOffset = 0, yOffset = 0;
        switch ( pop.getType() )
        {
          case Hamlet:
            popImage = pop.isOwned() ? _ownedHamlet : _hamlet;
            xScale = (int) (CELL_METRICS._width * 0.4);
            yScale = (int) (CELL_METRICS._height * 0.4);
            xOffset = (int) (CELL_METRICS._radius * 0.4);
            yOffset = (int) ((CELL_METRICS._height * 0.4) / 2);
            break;
          case Town:
          {
            popImage = pop.isOwned() ? _ownedTown : _town;
            xScale = (int) (CELL_METRICS._width * 0.4);
            yScale = (int) (CELL_METRICS._height * 0.4);
            xOffset = (int) (CELL_METRICS._radius * 0.4);
            yOffset = (int) ((CELL_METRICS._height * 0.4) / 2);
            break;
          }
          case City:
          {
            popImage = pop.isOwned() ? _ownedCity : _city;
            xScale = (int) (CELL_METRICS._width * 0.6);
            yScale = (int) (CELL_METRICS._height * 0.5);
            xOffset = (int) (CELL_METRICS._radius * 0.6);
            yOffset = (int) ((CELL_METRICS._height * 0.5) / 2);
            break;
          }
          case Unknown:
            break;
        }

        // Draw the pop center.
        if ( popImage != null )
        {
          if ( pop.isOwned() )
          {
            if ( pop.isCapitol() )
            {
              popImage = swapPixel( popImage, UNOWNED_SECONDARY_COLOR, Color.RED );
            }
            popImage = swapPixel( popImage, UNOWNED_MAIN_COLOR, _playerColors.get( pop.getOwner().getName() ) );
          }
          g.drawImage( popImage, pixelCenter.x - xOffset, pixelCenter.y - yOffset, xScale, yScale, null );
        }
      }

      // TODO - draw other items
    }
  }

  /** Paint any extra subclass specific stuff. */
  protected abstract void paintExtras( Graphics g );

  protected abstract List<GameItem> getGameItems();

  protected abstract List<GameItem> getGameItems( Tuple grid );

  protected abstract List<String> getRegionNames();

  protected Map<String, Color> assignRegionColors( List<String> regionNames )
  {
    Map<String, Color> regionColors = new HashMap<>();
    for ( int i = 0; i < regionNames.size(); i++ )
    {
      regionColors.put( regionNames.get( i ), REGION_COLORS.get( i ) );
    }
    return regionColors;
  }

  /** @return the array of trapezoids that define border areas to color. */
  public List<Polygon> getEdgeBordersToFill( Location location )
  {
    List<Polygon> borders = new ArrayList<>();
    if ( (location != null) && !location.getRegion().equals( Region.UNKNOWN_REGION ) )
    {
      Map<Tuple, Location> locations = getLocations();
      int[][] regionCorners = CELL_METRICS.computePixelCorners( location.getCoord().x, location.getCoord().y, 0 );
      for ( int direction = 0; direction < 6; direction++ )
      {
        Tuple neighbor = HexCalculator.getGridNeighbor( location.getCoord(), direction );
        Location adjacentLocation = locations.get( neighbor );
        boolean adjacentIsDifferentRegion =
          (adjacentLocation == null) || (adjacentLocation.getRegion() == null) ||
          !location.getRegion().equals( adjacentLocation.getRegion() );
        if ( adjacentIsDifferentRegion )
        {
          // Figure out the 4 corners near the edge of interest.
          int rightRightCornerX = regionCorners[0][(direction + 5) % 6];
          int rightCornerX = regionCorners[0][direction];
          int leftCornerX = regionCorners[0][(direction + 1) % 6];
          int leftLeftCornerX = regionCorners[0][(direction + 2) % 6];

          int rightRightCornerY = regionCorners[1][(direction + 5) % 6];
          int rightCornerY = regionCorners[1][direction];
          int leftCornerY = regionCorners[1][(direction + 1) % 6];
          int leftLeftCornerY = regionCorners[1][(direction + 2) % 6];

          // Figure out the trapezoid that defines the edge we want to fill.
          int[] x = new int[4];
          x[0] = rightCornerX;
          x[1] = leftCornerX;
          x[2] = leftCornerX + ((leftLeftCornerX - leftCornerX) / 4);
          x[3] = rightCornerX + ((rightRightCornerX - rightCornerX) / 4);
          int[] y = new int[4];
          y[0] = rightCornerY;
          y[1] = leftCornerY;
          y[2] = leftCornerY + ((leftLeftCornerY - leftCornerY) / 4);
          y[3] = rightCornerY + ((rightRightCornerY - rightCornerY) / 4);
          borders.add( new Polygon( x, y, 4 ) );
        }
      }
    }
    return borders;
  }

  /** Swap a given pixel color for a new one in an image, creating a new image in the process. */
  public BufferedImage swapPixel( BufferedImage oldImage, Color oldColor, Color newColor )
  {
    ColorModel cm = oldImage.getColorModel();
    WritableRaster raster = oldImage.copyData( null );
    Hashtable imageProperties = null;
    BufferedImage newImage = new BufferedImage( cm, raster, cm.isAlphaPremultiplied(), imageProperties );
    for ( int x = 0; x < newImage.getWidth(); x++ )
    {
      for ( int y = 0; y < newImage.getHeight(); y++ )
      {
        int rgb = newImage.getRGB( x, y );
        if ( rgb == oldColor.getRGB() )
        {
          newImage.setRGB( x, y, newColor.getRGB() );
        }
      }
    }
    return newImage;
  }

  public Map<String, Color> getPlayerColors() { return _playerColors; }
}
