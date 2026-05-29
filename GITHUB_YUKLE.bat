@echo off
set /p repo_url="Lutfen GitHub depo (repository) linkinizi yapistirin (ornek: https://github.com/kullanici/proje.git): "
git remote add origin %repo_url%
git branch -M main
git push -u origin main
echo.
echo Islem tamamlandi! GitHub sayfanizdaki 'Actions' sekmesine giderek build islemini takip edebilirsiniz.
pause
