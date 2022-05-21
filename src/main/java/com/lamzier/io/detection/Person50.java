package com.lamzier.io.detection;

import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.Joints;
import ai.djl.modality.cv.translator.SimplePoseTranslator;
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
 * 50p人检查器
 */
public class Person50 {

    private Predictor<Image, Joints> predictor;

    /**
     * 初始化
     */
    protected Person50(){
        Map<String, Object> arguments = new ConcurrentHashMap<>();
        arguments.put("width", 192);
        arguments.put("height", 256);
        arguments.put("resize", true);
        arguments.put("normalize", true);
        arguments.put("threshold", 0.2);
        Translator<Image, Joints> translator = SimplePoseTranslator.builder(arguments).build();
        Criteria<Image, Joints> criteria =
                Criteria.builder()
                        .optEngine("MXNet")
                        .setTypes(Image.class, Joints.class)
                        .optModelUrls(
//                                "https://aias-home.oss-cn-beijing.aliyuncs.com/models/simple_pose_resnet18_v1b.zip")//粗模型
                                "https://aias-home.oss-cn-beijing.aliyuncs.com/models/simple_pose_resnet50_v1b.zip")//细模型
                        // .optModelUrls("/Users/calvin/Documents/build/mxnet_models/simple_pose_resnet18_v1b/")
                        .optTranslator(translator)
                        .optProgress(new ProgressBar())
                        // .optDevice(Device.cpu())
                        .build();

        try {
            ZooModel<Image, Joints> pose = ModelZoo.loadModel(criteria);
            predictor = pose.newPredictor();
        } catch (Exception e) {
            e.printStackTrace();
            Logger logger = Logger.getLogger(Person50.class.getName());
            logger.log(Level.SEVERE , e.toString());
        }
    }


    /**
     * 预测
     */
    public Joints predict(Image image) throws TranslateException {
        return predictor.predict(image);
    }



}
