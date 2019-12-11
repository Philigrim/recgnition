# Instructions

Overleaf link'as: https://www.overleaf.com/9188281323xhctmrstcjvs

1. Atsisiųsti APK failą (rasite GIT'o pagrindinėje direktorijoje) ir suinstaliuoti savo telefone (Android OS), įeinant į programą pirmiausia nueitį į žemėlapį, suteikti location leidimą ir tik tada grįžti į kamerą atlikinėti atpažinimą.(SVARBU!!!)
2. Kodą rašėme naudodami Android Studio aplinką, taigi norėdami pakeisti mūsų kodą, turite suinstaliuoti Android Studio, įsikelti kodą iš GIT'o ir norėdami sukompiliuoti,
būkite tikri, jog savo telefone esate įsijungė 'Developer' rėžimą (kaip tą padaryti rasite Google). Prijungę savo telefoną su USB prie kompiuterio, Android Studio turėtų aptikti jūsų telefoną ir norėdami kodą sukompiliuoti telefone,
spauskite RUN. 
3. Norėdami prisijungti prie mūsų virtualios mašinos ir duomenų bazės terminale naudoti šias komandas: 1.ssh -L 5500:localhost:5432 -p 1574 neja5785@193.219.91.103 (slaptažodis Virtualizacija) 2.psql -d zenklu_valdovai -U user1 (slaptažodis test)
4. Norėdami matyti duomenų bazę vizualiai naudokite PgAdmin aplinką. Tik būkite tikri, jog esate prisijungę terminale prie ssh tunelio. PgAdmine ties host'u įvesti localhost, o porte 5500.