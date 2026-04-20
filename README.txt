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
