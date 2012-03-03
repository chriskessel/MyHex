package kessel.hex.util;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ColorPicker
{
  public static List<Color> chooseDistinguishableColors( int numberOfColors )
  {
    List<Color> colors = new ArrayList<>();
    float gap = 1.0f / (float) ((numberOfColors / 2) + (numberOfColors % 2));
    for ( int i = 0; colors.size() < numberOfColors; i++ )
    {
      float hue = i * gap;
      colors.add( new Color( Color.HSBtoRGB( hue, 1.0f, 0.90f ) ) );
      colors.add( new Color( Color.HSBtoRGB( hue, 1.0f, 0.65f ) ) );
    }
    return colors;
  }
}
