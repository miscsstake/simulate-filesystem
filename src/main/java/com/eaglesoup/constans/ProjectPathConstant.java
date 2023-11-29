package com.eaglesoup.constans;


import lombok.Data;

@Data
public class ProjectPathConstant {
    private static ProjectPathConstant projectPathConstant = new ProjectPathConstant();
    private String projectPath;

    //写个类单例
    public static ProjectPathConstant getInstance() {
        return projectPathConstant;
    }

    public String getInnerBinPath() {
        return getProjectPath() + "/applayer/bin/";
    }
}
