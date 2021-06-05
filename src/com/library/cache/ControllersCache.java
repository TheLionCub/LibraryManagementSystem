package com.library.cache;

import com.library.application.controllers.main.BookDetailsController;
import com.library.application.controllers.main.BookSearchController;
import com.library.application.controllers.main.MemberDetailsController;
import com.library.application.controllers.main.MemberSearchController;

public class ControllersCache {
    public static BookSearchController bookSearchController;
    public static MemberSearchController memberSearchController;
    public static BookDetailsController bookDetailsController;
    public static MemberDetailsController memberDetailsController;

    public static void clearCache() {
        bookSearchController = null;
        memberSearchController = null;
        bookDetailsController = null;
        memberDetailsController = null;
    }
}