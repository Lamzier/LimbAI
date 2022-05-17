package com.lamzier.io.detection;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.translator.SingleShotDetectionTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 人类检测器
 */
public class Person {

    private Predictor<Image, DetectedObjects> predictor;

    protected Person(){
        Map<String, Object> arguments = new ConcurrentHashMap<>();
        arguments.put("width", 512);
        arguments.put("height", 512);
        arguments.put("resize", true);
        arguments.put("rescale", true);
        arguments.put("threshold", 0.2);
        Translator<Image, DetectedObjects> translator =
                SingleShotDetectionTranslator.builder(arguments).build();
        Criteria<Image, DetectedObjects> criteria =
                Criteria.builder()
                        .optEngine("MXNet")
                        .setTypes(Image.class, DetectedObjects.class)
                        .optModelUrls(
                                "https://aias-home.oss-cn-beijing.aliyuncs.com/models/ssd_512_resnet50_v1_voc.zip")
//             .optModelUrls("/Users/calvin/Documents/build/mxnet_models/ssd_512_resnet50_v1_voc/")
                        .optTranslator(translator)
                        .optProgress(new ProgressBar())
                        // .optDevice(Device.cpu())
                        .build();

        try {
            ZooModel<Image, DetectedObjects> ssd = ModelZoo.loadModel(criteria);
            predictor = ssd.newPredictor();
        } catch (Exception e) {
            e.printStackTrace();
            Logger logger = Logger.getLogger(Person.class.getName());
            logger.log(Level.SEVERE , e.toString());
        }
    }

    /**
     * 预测
     */
    protected DetectedObjects predict(Image image) throws TranslateException {
        return predictor.predict(image);
    }

}
