Place le modèle MobileFaceNet TFLite ici sous le nom exact :

    mobilefacenet.tflite

Sources recommandées (choisir une variante entraînée avec perte ArcFace si possible) :
  - https://github.com/sirius-ai/MobileFaceNet_TF
  - https://github.com/estebanuri/face_recognition (releases → .tflite)
  - https://github.com/shubham0204/FaceRecognition_With_FaceNet_Android (variantes MobileFaceNet)

Contraintes attendues par FaceEmbedder.java :
  - Input  : 1 × 112 × 112 × 3 float32, normalisé (pixel - 127.5) / 128 → [-1, 1]
  - Output : 1 × 192 float32 (embedding)

Si la variante téléchargée a une autre taille d'embedding (ex. 128 ou 512), modifier
la constante EMBEDDING_SIZE dans FaceEmbedder.java.
