package com.mad.project.presenter;

/**
 * Created by Darren on 4/06/2018.
 */

public interface LoginPresenterInt {
    void clear();
    void doLogin(String name, String passwd);
    void setProgressBarVisiblity(int visiblity);
}
