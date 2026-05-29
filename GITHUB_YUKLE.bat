@echo off
:: Change directory to the script's location
cd /d "%~dp0"

echo Hayat Koprusu - GitHub Yukleme Araci
echo -----------------------------------
set /p repo_url="Lutfen GitHub depo linkinizi yapistirin: "

:: Initialize and upload
git init
git add .
git commit -m "Initial commit"
git remote add origin %repo_url%
git branch -M main
git push -u origin main -f

echo.
echo Islem tamamlandi! GitHub sayfanizdaki 'Actions' sekmesine giderek build islemini takip edebilirsiniz.
pause
