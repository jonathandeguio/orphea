package io.movetodata.snap.artifact.library.services;

import io.movetodata.snap.artifact.library.models.CheckUpdatesModel;
import org.springframework.stereotype.Component;

@Component
public class ArtifactService {

    public String getTagForComponent(CheckUpdatesModel model, String componentName) {
        switch (componentName) {
            case "boson":
                return model.getBoson();
            case "frontend":
                return model.getFrontend();
            case "parler":
                return model.getParler();
            case "julia":
                return model.getJulia();
            case "callisto":
                return model.getCallisto();
            case "capture":
                return model.getCapture();
            case "movetodata-docs":
                return model.getMoveToDataDocs();
            case "spark-history-server":
                return model.getSparkHistoryServer();
            default:
                return null; // Handle unknown component names
        }
    }

    public void setTagForComponent(CheckUpdatesModel model, String componentName, String tag) {
        switch (componentName) {
            case "boson":
                model.setBoson(tag);
                break;
            case "frontend":
                model.setFrontend(tag);
                break;
            case "parler":
                model.setParler(tag);
                break;
            case "julia":
                model.setJulia(tag);
                break;
            case "callisto":
                model.setCallisto(tag);
                break;
            case "capture":
                model.setCapture(tag);
                break;
            case "movetodata-docs":
                model.setMoveToDataDocs(tag);
                break;
            case "spark-history-server":
                model.setSparkHistoryServer(tag);
                break;
            default:
                // Handle unknown component names
                break;
        }
    }
}