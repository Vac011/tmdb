package edu.whu.tmdb.query.operations.utils;

import java.io.File;

import edu.whu.tmdb.App;

public class Constants {
    public static final String TORCH_RES_BASE_DIR= new File(App.context.getCacheDir(),"data/res").getAbsolutePath();
}

