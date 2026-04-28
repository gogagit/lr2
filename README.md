Лабораторная работа 2. Альбом впечатлений.

Что реализовано:
- просмотр изображений в виде слайд-шоу;
- выбор каталога через DirectoryChooser;
- рекурсивный обход папки с учетом вложенных каталогов;
- фильтрация по формату (*.jpg, *.png, *.bmp и др.);
- паттерн Итератор:
  * Aggregate -> com.example.photogallery.iterators.Aggregate
  * ConcreteAggregate -> com.example.photogallery.models.ConcreteAggregate
  * ImageIterator -> com.example.photogallery.iterators.ImageIterator
  * concrete iterator -> внутренний класс DirectoryImageIterator
- кнопки запуска, останова, вперед и назад;
- ImageView для показа слайдов;
- простая анимация смены кадра (FadeTransition);
- сохранена поддержка реакций через фабрики.

Основная точка входа:
com.example.photogallery.app.GalleryApp

## Окно работы программы
<img width="1772" height="1167" alt="image" src="https://github.com/user-attachments/assets/59661edf-1c75-4c12-9ea6-21e98e427912" />


## Диаграмма классов 
<img width="1280" height="1535" alt="image" src="https://github.com/user-attachments/assets/8337adee-e8fe-440d-819c-a13ebfcd4e79" />
