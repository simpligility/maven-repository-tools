package com.simpligility.maven.provisioner;

import java.io.File;
import java.io.Serializable;

import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;

public class VisibleDirectoryFileFilter extends AbstractFileFilter implements Serializable
{
  private static final long serialVersionUID = -2364729562384525732L;

  public static final IOFileFilter DIRECTORY = new VisibleDirectoryFileFilter();

  public static final IOFileFilter INSTANCE = DIRECTORY;

  protected VisibleDirectoryFileFilter()
  {
  }

  /**
   * Checks to see if the file is a directory and it is NOT a hidden directory.
   *
   * @param file  the File to check
   * @return true if the file is a directory
   */
  @Override
  public boolean accept( final File file )
  {
      boolean startsWithDot = !file.getName().isEmpty() && file.getName().startsWith( "." );
      boolean isVisible = !file.isHidden() && !startsWithDot;
      return file.isDirectory() && isVisible;
  }

}
