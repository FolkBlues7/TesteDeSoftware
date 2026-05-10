package views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import models.Usuario;
import java.util.List;

public class RankingView extends VBox {
    public RankingView(List<Usuario> usuarios, Runnable voltarAoMenu) {
        this.setPadding(new Insets(20));
        this.setSpacing(15);
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: #2c3e50;");

        Label titulo = new Label("RANKING GLOBAL");
        titulo.setFont(new Font("Arial", 24));
        titulo.setStyle("-fx-text-fill: #f1c40f; -fx-font-weight: bold;");

        // Criando uma tabela simples
        TextArea listaRanking = new TextArea();
        listaRanking.setEditable(false);
        listaRanking.setPrefHeight(300);
        listaRanking.setMaxWidth(450); // Ajustando a largura

        StringBuilder sb = new StringBuilder();
        // Cabeçalho atualizado sem o Avatar
        sb.append(String.format("%-15s | %-10s | %-10s\n", "Login", "Pontos", "Sessões"));
        sb.append("-------------------------------------------\n");

        // Ordena por pontuação antes de exibir e remove a chamada ao getAvatar()
        usuarios.stream()
                .sorted((u1, u2) -> Integer.compare(u2.getPontuacaoTotal(), u1.getPontuacaoTotal()))
                .forEach(u -> sb.append(String.format("%-15s | %-10d | %-10d\n",
                        u.getLogin(), u.getPontuacaoTotal(), u.getSessoesExecutadas())));

        listaRanking.setText(sb.toString());
        listaRanking.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 14;");

        Button btnVoltar = new Button("Voltar");
        btnVoltar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnVoltar.setOnAction(e -> voltarAoMenu.run());

        this.getChildren().addAll(titulo, listaRanking, btnVoltar);
    }
}