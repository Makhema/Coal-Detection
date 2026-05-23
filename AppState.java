package acsse.csc3a.app;

import java.io.File;
import java.io.IOException;

import acsse.csc3a.classifier.AnalysisResult;
import acsse.csc3a.service.CoalAnalysisService;

/**
 * This class is responsible for shared application state.
 * The upload, graph and results panes all use this one state object so that the selected image and latest analysis result stay in sync.
 */
public class AppState {
    private static final AppState INSTANCE = new AppState();

    private final CoalAnalysisService analysisService = new CoalAnalysisService();
    private AnalysisResult latestResult;
    private String startUpMessage = "Dataset not loaded yet.";

    private AppState() {
    }

    public static AppState getInstance() {
        return INSTANCE;
    }

    public void initialise() {
        try
        {
            analysisService.initialise();
            startUpMessage = "Dataset loaded successfully.";
        } 
        catch (IOException ex) 
        {
            startUpMessage = "Faield to load Dataset: " + ex.getMessage();
        }
    }

    public AnalysisResult analyseExternalImage(File file) throws IOException {
        latestResult = analysisService.analyseExternalImage(file);
        return latestResult;
    }

    public AnalysisResult getLatestResult() {
        return latestResult;
    }

    public String getStartUpMessage() {
        return startUpMessage;
    }
}
