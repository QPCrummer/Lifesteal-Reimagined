package com.github.qpcrummer.lifesteal_reimagined.utils;

public interface PlayerReviveData {
    // This means that hearts shouldn't be given if killed
    boolean newlyRevived();
    void setNewlyRevived(boolean set);
}
