import os
import Augmentor


def augment_images_in_subdirectories(in_directory, sample_size):
    # Prođite kroz sve poddirektorije unutar in_directory
    for subdir in os.listdir(in_directory):
        subdir_path = os.path.join(in_directory, subdir)

        # Provjerite je li poddirektorij
        if os.path.isdir(subdir_path):
            # Kreirajte pipeline za augmentaciju
            p = Augmentor.Pipeline(source_directory=subdir_path, output_directory=subdir_path)

            # Dodajte operacije augmentacije
            p.rotate(probability=0.7, max_left_rotation=25, max_right_rotation=25)
            p.zoom_random(probability=0.5, percentage_area=0.7)
            p.random_contrast(probability=0.5, min_factor=0.8, max_factor=1.2)
            p.random_brightness(probability=0.5, min_factor=0.8, max_factor=1.2)
            p.random_color(probability=0.5, min_factor=0.8, max_factor=1.2)

            p.set_save_format("png")

            # Generirajte augmentirane slike
            p.sample(sample_size)


# Koristite metodu
in_directory = "C:\\Faks\\numbers_reader\\pre_processed_images"
augment_images_in_subdirectories(in_directory, sample_size=700)
